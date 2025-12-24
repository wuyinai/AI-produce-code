// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 开始协作 POST /collaboration/start/${param0} */
export async function startCollaboration(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.startCollaborationParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseLong>(`/collaboration/start/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 添加协作者 POST /collaboration/add/${param0} */
export async function addCollaborator(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.addCollaboratorParams,
  body: API.CollaborationRequest,
  options?: { [key: string]: any }
) {
  const { collaborationId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/collaboration/add/${param0}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  })
}

/** 退出协作 POST /collaboration/exit/${param0} */
export async function exitCollaboration(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.exitCollaborationParams,
  options?: { [key: string]: any }
) {
  const { collaborationId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/collaboration/exit/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 获取协作记录 GET /collaboration/record/${param0} */
export async function getCollaborationRecord(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCollaborationRecordParams,
  options?: { [key: string]: any }
) {
  const { collaborationId: param0, ...queryParams } = params
  return request<API.BaseResponseCollaborationRecord>(`/collaboration/record/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 根据应用ID获取协作记录 GET /collaboration/record/by-app/${param0} */
export async function getCollaborationRecordByAppId(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCollaborationRecordByAppIdParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseCollaborationRecord>(`/collaboration/record/by-app/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 获取在线好友列表 GET /collaboration/online-friends */
export async function getOnlineFriends(
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseUserVO[]>(`/collaboration/online-friends`, {
    method: 'GET',
    ...(options || {}),
  })
}

/** 获取协作成员列表 GET /collaboration/members/${param0} */
export async function getCollaborationMembers(
  params: API.getCollaborationRecordParams,
  options?: { [key: string]: any }
) {
  const { collaborationId: param0, ...queryParams } = params
  return request<API.BaseResponseListCollaborationMember>(`/collaboration/members/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}
