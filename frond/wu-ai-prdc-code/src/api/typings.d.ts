declare namespace API {
  type acceptFriendRequestParams = {
    requestId: number
  }

  type addCollaboratorParams = {
    collaborationId: number
  }

  type AppAddRequest = {
    initPrompt?: string
  }

  type AppAdminUpdateRequest = {
    id?: number
    appName?: string
    cover?: string
    priority?: number
  }

  type AppDeployRequest = {
    appId?: number
  }

  type AppQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    priority?: number
    userId?: number
    status?: string
  }

  type AppSaveDirectEditRequest = {
    appId?: number
    files?: EditedFile[]
  }

  type AppUpdateRequest = {
    id?: number
    appName?: string
  }

  type AppVersionCreateRequest = {
    appId?: number
    triggerType?: string
    triggerMessage?: string
    versionDescription?: string
  }

  type AppVersionVO = {
    id?: number
    appId?: number
    versionNumber?: number
    versionName?: string
    versionDescription?: string
    triggerType?: string
    triggerMessage?: string
    isCurrent?: number
    createTime?: string
    createUserId?: number
    createUserName?: string
  }

  type AppVO = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: number
    createTime?: string
    updateTime?: string
    user?: UserVO
    status?: string
  }

  type BaseResponseAppVersionVO = {
    code?: number
    data?: AppVersionVO
    message?: string
  }

  type BaseResponseAppVO = {
    code?: number
    data?: AppVO
    message?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseCollaborationRecord = {
    code?: number
    data?: CollaborationRecord
    message?: string
  }

  type BaseResponseFriendVO = {
    code?: number
    data?: FriendVO
    message?: string
  }

  type BaseResponseListAppVersionVO = {
    code?: number
    data?: AppVersionVO[]
    message?: string
  }

  type BaseResponseListCollaborationMemberVO = {
    code?: number
    data?: CollaborationMemberVO[]
    message?: string
  }

  type BaseResponseListFriendRequestVO = {
    code?: number
    data?: FriendRequestVO[]
    message?: string
  }

  type BaseResponseListFriendVO = {
    code?: number
    data?: FriendVO[]
    message?: string
  }

  type BaseResponseListSourceCodeFileDTO = {
    code?: number
    data?: SourceCodeFileDTO[]
    message?: string
  }

  type BaseResponseListUser = {
    code?: number
    data?: User[]
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponseObject = {
    code?: number
    data?: Record<string, any>
    message?: string
  }

  type BaseResponsePageAppVO = {
    code?: number
    data?: PageAppVO
    message?: string
  }

  type BaseResponsePageChatHistory = {
    code?: number
    data?: PageChatHistory
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type ChatHistory = {
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type ChatHistoryQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    lastCreateTime?: string
  }

  type chatToGenCodeParams = {
    appId: number
    message: string
  }

  type CollaborationMemberVO = {
    id?: number
    collaborationId?: number
    userId?: number
    userName?: string
    joinTime?: string
    createTime?: string
  }

  type CollaborationRecord = {
    id?: number
    appId?: number
    creatorId?: number
    status?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type CollaborationRequest = {
    userId?: number
  }

  type deleteFriendParams = {
    friendId: number
  }

  type DeleteRequest = {
    id?: number
  }

  type deleteVersionParams = {
    versionId: number
  }

  type downloadProjectParams = {
    appId: number
  }

  type EditedFile = {
    filePath?: string
    content?: string
  }

  type exitCollaborationParams = {
    collaborationId: number
  }

  type FriendRequestDTO = {
    toUserId?: number
    message?: string
  }

  type FriendRequestVO = {
    id?: number
    fromUserId?: number
    fromUserName?: string
    fromUserAvatar?: string
    fromUserAccount?: string
    toUserId?: number
    message?: string
    status?: string
    createTime?: string
  }

  type FriendVO = {
    id?: number
    userName?: string
    userAvatar?: string
    userAccount?: string
    userProfile?: string
    status?: string
    createTime?: string
    onlineStatus?: string
  }

  type getAppSourceDirParams = {
    appId: number
  }

  type getAppSourceFileParams = {
    appId: number
    filePath: string
  }

  type getAppSourceParams = {
    appId: number
  }

  type getAppVOByIdByAdminParams = {
    id: number
  }

  type getAppVOByIdParams = {
    id: number
  }

  type getCollaborationMembersParams = {
    collaborationId: number
  }

  type getCollaborationRecordByAppIdParams = {
    appId: number
  }

  type getCollaborationRecordParams = {
    collaborationId: number
  }

  type getCollaboratorsByAppIdParams = {
    appId: number
  }

  type getCurrentVersionParams = {
    appId: number
  }

  type getFriendDetailParams = {
    friendId: number
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type getVersionSnapshotParams = {
    versionId: number
  }

  type listAppChatHistoryParams = {
    appId: number
    pageSize?: number
    lastCreateTime?: string
  }

  type listVersionsParams = {
    appId: number
  }

  type LoginUserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    onlineStatus?: string
    createTime?: string
    updateTime?: string
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatHistory = {
    records?: ChatHistory[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type rejectFriendRequestParams = {
    requestId: number
  }

  type rollbackToVersionParams = {
    versionId: number
  }

  type saveSourceFileParams = {
    appId: number
  }

  type searchUsersParams = {
    keyword: string
  }

  type ServerSentEventString = true

  type serveStaticResourceParams = {
    deployKey: string
  }

  type SourceCodeFileDTO = {
    name?: string
    path?: string
    isDir?: boolean
    ext?: string
    size?: number
  }

  type SourceCodeSaveRequest = {
    filePath?: string
    content?: string
  }

  type startCollaborationParams = {
    appId: number
  }

  type User = {
    id?: number
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    onlineStatus?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserAddRequest = {
    userName?: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserUpdateRequest = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    onlineStatus?: string
    createTime?: string
  }
}
