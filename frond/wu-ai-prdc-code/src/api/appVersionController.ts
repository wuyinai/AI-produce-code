// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /app/version/create */
export async function createVersion(
  body: API.AppVersionCreateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong>('/app/version/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/version/current/${param0} */
export async function getCurrentVersion(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCurrentVersionParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseAppVersionVO>(`/app/version/current/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /app/version/delete/${param0} */
export async function deleteVersion(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteVersionParams,
  options?: { [key: string]: any }
) {
  const { versionId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/app/version/delete/${param0}`, {
    method: 'DELETE',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/version/list/${param0} */
export async function listVersions(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listVersionsParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseListAppVersionVO>(`/app/version/list/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/version/rollback/${param0} */
export async function rollbackToVersion(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.rollbackToVersionParams,
  options?: { [key: string]: any }
) {
  const { versionId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/app/version/rollback/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/version/snapshot/${param0} */
export async function getVersionSnapshot(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getVersionSnapshotParams,
  options?: { [key: string]: any }
) {
  const { versionId: param0, ...queryParams } = params
  return request<API.BaseResponseString>(`/app/version/snapshot/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}
