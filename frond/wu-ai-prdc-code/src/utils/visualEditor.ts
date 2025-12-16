/**
 * 可视化编辑器工具类
 * 负责管理iframe内的可视化编辑功能
 */
export interface ElementInfo {
  tagName: string
  id: string
  className: string
  textContent: string
  selector: string
  pagePath: string
  rect: {
    top: number
    left: number
    width: number
    height: number
  }
}

export interface VisualEditorOptions {
  onElementSelected?: (elementInfo: ElementInfo) => void
  onElementHover?: (elementInfo: ElementInfo) => void
}

export class VisualEditor {
  private iframe: HTMLIFrameElement | null = null
  private isEditMode = false
  private isDirectEditMode = false // 添加直接修改模式标志
  private options: VisualEditorOptions

  constructor(options: VisualEditorOptions = {}) {
    this.options = options
  }

  /**
   * 初始化编辑器
   */
  init(iframe: HTMLIFrameElement) {
    this.iframe = iframe
  }

  /**
   * 开启编辑模式
   */
  enableEditMode() {
    if (!this.iframe) {
      return
    }
    this.isEditMode = true
    setTimeout(() => {
      this.injectEditScript()
    }, 300)
  }

  /**
   * 关闭编辑模式
   */
  disableEditMode() {
    this.isEditMode = false
    this.sendMessageToIframe({
      type: 'TOGGLE_EDIT_MODE',
      editMode: false,
    })
    // 清除所有编辑状态
    this.sendMessageToIframe({
      type: 'CLEAR_ALL_EFFECTS',
    })
  }

  /**
   * 切换编辑模式
   */
  toggleEditMode() {
    if (this.isEditMode) {
      this.disableEditMode()
    } else {
      this.enableEditMode()
    }
    return this.isEditMode
  }

  /**
   * 开启直接修改模式
   */
  enableDirectEditMode() {
    if (!this.iframe) {
      return
    }
    this.isDirectEditMode = true
    // 如果还没有开启编辑模式，先开启编辑模式
    if (!this.isEditMode) {
      this.enableEditMode()
    }
    this.sendMessageToIframe({
      type: 'ENABLE_DIRECT_EDIT',
      directEditMode: true,
    })
  }

  /**
   * 关闭直接修改模式
   */
  disableDirectEditMode() {
    this.isDirectEditMode = false
    this.sendMessageToIframe({
      type: 'ENABLE_DIRECT_EDIT',
      directEditMode: false,
    })
  }

  /**
   * 切换直接修改模式
   */
  toggleDirectEditMode() {
    if (this.isDirectEditMode) {
      this.disableDirectEditMode()
    } else {
      this.enableDirectEditMode()
    }
    return this.isDirectEditMode
  }

  /**
   * 保存直接修改的内容
   * @returns Promise<boolean> 是否保存成功
   */
  async saveDirectEdit() {
    if (!this.iframe) {
      return false
    }

    try {
      // 发送消息到iframe，获取修改的文件内容
      const modifiedFiles = await this.sendMessageToIframeAndWaitResponse({
        type: 'GET_MODIFIED_FILES'
      })
      
      return modifiedFiles || []
    } catch (error) {
      console.error('保存直接修改内容失败:', error)
      return false
    }
  }

  /**
   * 向iframe发送消息并等待响应
   * @param message 要发送的消息
   * @returns Promise<any> 响应结果
   */
  private sendMessageToIframeAndWaitResponse(message: Record<string, any>): Promise<any> {
    return new Promise((resolve, reject) => {
      if (!this.iframe?.contentWindow) {
        reject(new Error('Iframe not ready'))
        return
      }

      // 生成唯一的请求ID
      const requestId = `request_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
      message.requestId = requestId

      // 定义响应处理函数
      const handleResponse = (event: MessageEvent) => {
        if (event.data && event.data.type === 'RESPONSE' && event.data.requestId === requestId) {
          window.removeEventListener('message', handleResponse)
          resolve(event.data.data)
        }
      }

      // 添加事件监听器
      window.addEventListener('message', handleResponse)

      // 发送消息
      this.iframe.contentWindow.postMessage(message, '*')

      // 设置超时
      setTimeout(() => {
        window.removeEventListener('message', handleResponse)
        reject(new Error('Request timed out'))
      }, 5000)
    })
  }

  /**
   * 强制同步状态并清理
   */
  syncState() {
    if (!this.isEditMode) {
      this.sendMessageToIframe({
        type: 'CLEAR_ALL_EFFECTS',
      })
    }
  }

  /**
   * 清除选中的元素
   */
  clearSelection() {
    this.sendMessageToIframe({
      type: 'CLEAR_SELECTION',
    })
  }

  /**
   * iframe 加载完成时调用
   */
  onIframeLoad() {
    if (this.isEditMode) {
      setTimeout(() => {
        this.injectEditScript()
      }, 500)
    } else {
      // 确保非编辑模式时清理状态
      setTimeout(() => {
        this.syncState()
      }, 500)
    }
  }

  /**
   * 处理来自 iframe 的消息
   */
  handleIframeMessage(event: MessageEvent) {
    const { type, data } = event.data
    switch (type) {
      case 'ELEMENT_SELECTED':
        if (this.options.onElementSelected && data.elementInfo) {
          this.options.onElementSelected(data.elementInfo)
        }
        break
      case 'ELEMENT_HOVER':
        if (this.options.onElementHover && data.elementInfo) {
          this.options.onElementHover(data.elementInfo)
        }
        break
    }
  }

  /**
   * 向 iframe 发送消息
   */
  private sendMessageToIframe(message: Record<string, any>) {
    if (this.iframe?.contentWindow) {
      this.iframe.contentWindow.postMessage(message, '*')
    }
  }

  /**
   * 注入编辑脚本到 iframe
   */
  private injectEditScript() {
    if (!this.iframe) return

    const waitForIframeLoad = () => {
      try {
        if (this.iframe!.contentWindow && this.iframe!.contentDocument) {
          // 检查是否已经注入过脚本
          if (this.iframe!.contentDocument.getElementById('visual-edit-script')) {
            this.sendMessageToIframe({
              type: 'TOGGLE_EDIT_MODE',
              editMode: true,
            })
            return
          }

          const script = this.generateEditScript()
          const scriptElement = this.iframe!.contentDocument.createElement('script')
          scriptElement.id = 'visual-edit-script'
          scriptElement.textContent = script
          this.iframe!.contentDocument.head.appendChild(scriptElement)
        } else {
          setTimeout(waitForIframeLoad, 100)
        }
      } catch {
        // 静默处理注入失败
      }
    }

    waitForIframeLoad()
  }

  /**
   * 生成编辑脚本内容
   */
  private generateEditScript() {
    return `
      (function() {
        let isEditMode = true;
        let isDirectEditMode = false; // 添加直接修改模式标志
        let currentHoverElement = null;
        let currentSelectedElement = null;
        let currentEditingElement = null; // 当前正在编辑的元素

        // 直接修改相关功能
        function enableDirectEdit() {
          isDirectEditMode = true;
          showDirectEditTip();
        }

        function disableDirectEdit() {
          isDirectEditMode = false;
          // 清理正在编辑的元素
          if (currentEditingElement) {
            finishEditing();
          }
        }

        // 开始编辑元素内容
        function startEditing(element) {
          if (!isDirectEditMode || !element || element.tagName === 'INPUT' || element.tagName === 'TEXTAREA') {
            return;
          }

          // 确保元素是可编辑的
          if (!element.isContentEditable) {
            element.setAttribute('data-original-content', element.textContent);
            element.setAttribute('contenteditable', 'true');
            element.classList.add('direct-editing');
            currentEditingElement = element;
            
            // 聚焦到元素并全选内容
            setTimeout(() => {
              element.focus();
              const range = document.createRange();
              range.selectNodeContents(element);
              const selection = window.getSelection();
              selection?.removeAllRanges();
              selection?.addRange(range);
            }, 100);
          }
        }

        // 结束编辑元素内容
        function finishEditing() {
          if (currentEditingElement) {
            currentEditingElement.setAttribute('contenteditable', 'false');
            currentEditingElement.classList.remove('direct-editing');
            currentEditingElement = null;
          }
        }

        // 准备保存的内容，清除所有编辑相关的样式
        function prepareSaveContent() {
          // 1. 移除所有编辑相关的样式类
          const elements = document.querySelectorAll('.edit-hover, .edit-selected, .direct-editing');
          elements.forEach(el => {
            el.classList.remove('edit-hover', 'edit-selected', 'direct-editing');
          });
          
          // 2. 移除所有元素的contenteditable属性
          const editableElements = document.querySelectorAll('[contenteditable="true"]');
          editableElements.forEach(el => {
            el.removeAttribute('contenteditable');
          });
          
          // 3. 移除所有编辑相关的样式元素
          const styleElements = document.querySelectorAll('style');
          styleElements.forEach(style => {
            // 检查样式内容是否包含编辑模式相关的样式
            if (style.textContent && (style.textContent.includes('edit-hover') || style.textContent.includes('edit-selected') || style.textContent.includes('direct-editing'))) {
              style.remove();
            }
          });
          
          // 4. 移除编辑提示元素
          const editTip = document.getElementById('edit-tip');
          if (editTip) {
            editTip.remove();
          }
          
          const directEditTip = document.getElementById('direct-edit-tip');
          if (directEditTip) {
            directEditTip.remove();
          }
          
          // 5. 获取整个文档的HTML内容
          const content = document.documentElement.outerHTML;
          
          // 6. 将修改的文件内容保存到map中
          const filePath = 'index.html';
          modifiedFiles.set(filePath, content);
        }

        // 显示直接编辑提示
        function showDirectEditTip() {
          if (document.getElementById('direct-edit-tip')) return;
          const tip = document.createElement('div');
          tip.id = 'direct-edit-tip';
          tip.innerHTML = '✏️ 直接修改模式已开启<br/>点击文字即可直接编辑，按Enter或点击其他区域保存';
          tip.style.cssText = \`
            position: fixed;
            top: 70px;
            right: 20px;
            background: #52c41a;
            color: white;
            padding: 12px 16px;
            border-radius: 6px;
            font-size: 14px;
            z-index: 9999;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            animation: fadeIn 0.3s ease;
          \`;
          document.body.appendChild(tip);
          setTimeout(() => {
            if (tip.parentNode) {
              tip.style.animation = 'fadeIn 0.3s ease reverse';
              setTimeout(() => tip.remove(), 300);
            }
          }, 4000);
        }

        function injectStyles() {
          if (document.getElementById('edit-mode-styles')) return;
          const style = document.createElement('style');
          style.id = 'edit-mode-styles';
          style.textContent = \`
            .edit-hover {
              outline: 2px dashed #1890ff !important;
              outline-offset: 2px !important;
              cursor: crosshair !important;
              transition: outline 0.2s ease !important;
              position: relative !important;
            }
            .edit-hover::before {
              content: '' !important;
              position: absolute !important;
              top: -4px !important;
              left: -4px !important;
              right: -4px !important;
              bottom: -4px !important;
              background: rgba(24, 144, 255, 0.02) !important;
              pointer-events: none !important;
              z-index: -1 !important;
            }
            .edit-selected {
              outline: 3px solid #52c41a !important;
              outline-offset: 2px !important;
              cursor: default !important;
              position: relative !important;
            }
            .edit-selected::before {
              content: '' !important;
              position: absolute !important;
              top: -4px !important;
              left: -4px !important;
              right: -4px !important;
              bottom: -4px !important;
              background: rgba(82, 196, 26, 0.03) !important;
              pointer-events: none !important;
              z-index: -1 !important;
            }
            /* 直接修改模式样式 */
            [contenteditable="true"] {
              outline: 2px solid #faad14 !important;
              background-color: rgba(250, 173, 20, 0.05) !important;
            }
            
            [contenteditable="true"]:focus {
              outline: 3px solid #fa8c16 !important;
              background-color: rgba(250, 173, 20, 0.1) !important;
            }
            
            .direct-editing {
              outline: 2px solid #faad14 !important;
            }
          \`;
          document.head.appendChild(style);
        }

        // 生成元素选择器
        function generateSelector(element) {
          const path = [];
          let current = element;
          while (current && current !== document.body) {
            let selector = current.tagName.toLowerCase();
            if (current.id) {
              selector += '#' + current.id;
              path.unshift(selector);
              break;
            }
            if (current.className) {
              const classes = current.className.split(' ').filter(c => c && !c.startsWith('edit-'));
              if (classes.length > 0) {
                selector += '.' + classes.join('.');
              }
            }
            const siblings = Array.from(current.parentElement?.children || []);
            const index = siblings.indexOf(current) + 1;
            selector += ':nth-child(' + index + ')';
            path.unshift(selector);
            current = current.parentElement;
          }
          return path.join(' > ');
        }

        // 获取元素信息
        function getElementInfo(element) {
          const rect = element.getBoundingClientRect();
          // 获取 HTML 文件名后面的部分（查询参数和锚点）
          let pagePath = window.location.search + window.location.hash;
          // 如果没有查询参数和锚点，则显示为空
          if (!pagePath) {
            pagePath = '';
          }

          return {
            tagName: element.tagName,
            id: element.id,
            className: element.className,
            textContent: element.textContent?.trim().substring(0, 100) || '',
            selector: generateSelector(element),
            pagePath: pagePath,
            rect: {
              top: rect.top,
              left: rect.left,
              width: rect.width,
              height: rect.height
            }
          };
        }

        // 清除悬浮效果
        function clearHoverEffect() {
          if (currentHoverElement) {
            currentHoverElement.classList.remove('edit-hover');
            currentHoverElement = null;
          }
        }

        // 清除选中效果
        function clearSelectedEffect() {
          const selected = document.querySelectorAll('.edit-selected');
          selected.forEach(el => el.classList.remove('edit-selected'));
          currentSelectedElement = null;
        }

        let eventListenersAdded = false;

        function addEventListeners() {
           if (eventListenersAdded) return;

           const mouseoverHandler = (event) => {
             if (!isEditMode) return;

             const target = event.target;
             if (target === currentHoverElement || target === currentSelectedElement) return;
             if (target === document.body || target === document.documentElement) return;
             if (target.tagName === 'SCRIPT' || target.tagName === 'STYLE') return;

             clearHoverEffect();
             target.classList.add('edit-hover');
             currentHoverElement = target;
           };

           const mouseoutHandler = (event) => {
             if (!isEditMode) return;

             const target = event.target;
             if (!event.relatedTarget || !target.contains(event.relatedTarget)) {
               clearHoverEffect();
             }
           };

           const clickHandler = (event) => {
             if (!isEditMode) return;

             event.preventDefault();
             event.stopPropagation();

             const target = event.target;
             if (target === document.body || target === document.documentElement) return;
             if (target.tagName === 'SCRIPT' || target.tagName === 'STYLE') return;

             // 如果处于直接修改模式，且点击的是文本节点的父元素，则开始编辑
             if (isDirectEditMode) {
               // 先结束当前编辑
               if (currentEditingElement) {
                 finishEditing();
               }
               // 开始新的编辑
               startEditing(target);
               return;
             }

             // 现有选择逻辑
             clearSelectedEffect();
             clearHoverEffect();

             target.classList.add('edit-selected');
             currentSelectedElement = target;

             const elementInfo = getElementInfo(target);
             try {
               window.parent.postMessage({
                 type: 'ELEMENT_SELECTED',
                 data: { elementInfo }
               }, '*');
             } catch {
               // 静默处理发送失败
             }
           };

           // 添加失去焦点事件监听
           const blurHandler = (event) => {
             if (isDirectEditMode && currentEditingElement) {
               finishEditing();
             }
           };

           // 添加按键事件监听
           const keydownHandler = (event) => {
             if (isDirectEditMode && currentEditingElement) {
               // 按Enter键保存编辑
               if (event.key === 'Enter') {
                 event.preventDefault();
                 finishEditing();
               }
               // 按Esc键取消编辑
               if (event.key === 'Escape') {
                 event.preventDefault();
                 // 恢复原始内容
                 const originalContent = currentEditingElement.getAttribute('data-original-content');
                 if (originalContent) {
                   currentEditingElement.textContent = originalContent;
                 }
                 finishEditing();
               }
             }
           };

           // 现有事件监听保持不变
           document.body.addEventListener('mouseover', mouseoverHandler, true);
           document.body.addEventListener('mouseout', mouseoutHandler, true);
           document.body.addEventListener('click', clickHandler, true);
           
           // 添加新的事件监听
           document.body.addEventListener('blur', blurHandler, true);
           document.body.addEventListener('keydown', keydownHandler, true);
           
           eventListenersAdded = true;
         }

         function setupEventListeners() {
           addEventListeners();
         }

        // 跟踪修改的文件
        const modifiedFiles = new Map();

        // 发送响应消息到父窗口
        function sendResponse(message, data) {
          if (message.requestId) {
            window.parent.postMessage({
              type: 'RESPONSE',
              requestId: message.requestId,
              data: data
            }, '*');
          }
        }

        // 监听父窗口消息
        window.addEventListener('message', (event) => {
           const { type, editMode, directEditMode, requestId } = event.data;
           switch (type) {
             // 现有消息处理保持不变
             case 'TOGGLE_EDIT_MODE':
               isEditMode = editMode;
               if (isEditMode) {
                 injectStyles();
                 setupEventListeners();
                 showEditTip();
               } else {
                 clearHoverEffect();
                 clearSelectedEffect();
                 disableDirectEdit(); // 退出编辑模式时，同时退出直接修改模式
               }
               break;
             
             // 添加直接修改模式消息处理
             case 'ENABLE_DIRECT_EDIT':
               if (directEditMode) {
                 enableDirectEdit();
               } else {
                 disableDirectEdit();
               }
               break;
               
             case 'CLEAR_SELECTION':
               clearSelectedEffect();
               break;
               
             case 'CLEAR_ALL_EFFECTS':
               isEditMode = false;
               disableDirectEdit(); // 清除所有效果时，同时退出直接修改模式
               clearHoverEffect();
               clearSelectedEffect();
               const tip = document.getElementById('edit-tip');
               if (tip) tip.remove();
               const directTip = document.getElementById('direct-edit-tip');
               if (directTip) directTip.remove();
               break;
             
             // 添加获取修改文件内容的消息处理
             case 'GET_MODIFIED_FILES':
               // 准备保存的内容，清除所有编辑相关的样式
               prepareSaveContent();
               // 返回修改的文件列表
               const files = Array.from(modifiedFiles.entries()).map(([filePath, content]) => ({
                 filePath,
                 content
               }));
               sendResponse(event.data, files);
               break;
           }
         });

         function showEditTip() {
           if (document.getElementById('edit-tip')) return;
           const tip = document.createElement('div');
           tip.id = 'edit-tip';
           tip.innerHTML = '🎯 编辑模式已开启<br/>悬浮查看元素，点击选中元素';
           tip.style.cssText = \`
             position: fixed;
             top: 20px;
             right: 20px;
             background: #1890ff;
             color: white;
             padding: 12px 16px;
             border-radius: 6px;
             font-size: 14px;
             z-index: 9999;
             box-shadow: 0 4px 12px rgba(0,0,0,0.15);
             animation: fadeIn 0.3s ease;
           \`;
           const style = document.createElement('style');
           style.textContent = '@keyframes fadeIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }';
           document.head.appendChild(style);
           document.body.appendChild(tip);
           setTimeout(() => {
             if (tip.parentNode) {
               tip.style.animation = 'fadeIn 0.3s ease reverse';
               setTimeout(() => tip.remove(), 300);
             }
           }, 3000);
         }
         injectStyles();
         setupEventListeners();
         showEditTip();
      })();
    `
  }
}
