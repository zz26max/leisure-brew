import request from '@/utils/request'
/**
 *
 * 套餐管理
 *
 **/

// 查询列表数据
export const getSetmealPage = (params: any) => {
  return request({
    url: '/combo/page',
    method: 'get',
    params,
  },)
}

// 删除数据接口
export const deleteSetmeal = (ids: string) => {
  return request({
    url: '/combo',
    method: 'delete',
    params: { ids }
  })
}

// 修改数据接口
export const editSetmeal = (params: any) => {
  return request({
    url: '/combo',
    method: 'put',
    data: { ...params }
  })
}

// 新增数据接口
export const addSetmeal = (params: any) => {
  return request({
    url: '/combo',
    method: 'post',
    data: { ...params }
  })
}

// 查询详情接口
export const querySetmealById = (id: string | (string | null)[]) => {
  return request({
    url: `/combo/${id}`,
    method: 'get'
  })
}

// 批量上架下架
export const setmealStatusByStatus = (params: any) => {
  return request({
    url: `/combo/status/${params.status}`,
    method: 'post',
    params: { id: params.ids }
  })
}

//饮品分类数据查询
export const dishCategoryList = (params: any) => {
  return request({
    url: `/category/list`,
    method: 'get',
    params: { ...params }
  })
}
