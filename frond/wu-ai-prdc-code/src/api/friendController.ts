// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /friend/${param0} */
export async function getFriendDetail(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getFriendDetailParams,
  options?: { [key: string]: any }
) {
  const { friendId: param0, ...queryParams } = params
  return request<API.BaseResponseFriendVO>(`/friend/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /friend/${param0} */
export async function deleteFriend(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteFriendParams,
  options?: { [key: string]: any }
) {
  const { friendId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/friend/${param0}`, {
    method: 'DELETE',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /friend/list */
export async function getFriendList(options?: { [key: string]: any }) {
  return request<API.BaseResponseListFriendVO>('/friend/list', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /friend/request */
export async function sendFriendRequest(
  body: API.FriendRequestDTO,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/friend/request', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /friend/request/${param0}/accept */
export async function acceptFriendRequest(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.acceptFriendRequestParams,
  options?: { [key: string]: any }
) {
  const { requestId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/friend/request/${param0}/accept`, {
    method: 'PUT',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /friend/request/${param0}/reject */
export async function rejectFriendRequest(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.rejectFriendRequestParams,
  options?: { [key: string]: any }
) {
  const { requestId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/friend/request/${param0}/reject`, {
    method: 'PUT',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /friend/request/list */
export async function getReceivedFriendRequests(options?: { [key: string]: any }) {
  return request<API.BaseResponseListFriendRequestVO>('/friend/request/list', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /friend/request/sent */
export async function getSentFriendRequests(options?: { [key: string]: any }) {
  return request<API.BaseResponseListFriendRequestVO>('/friend/request/sent', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /friend/search */
export async function searchUsers(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchUsersParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListFriendVO>('/friend/search', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
