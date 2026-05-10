import request from '@/utils/request'
/**
 *
 * 饮品管理
 *
 **/
// 查询列表接口
export const getDishPage = (params: any) => {
  return request({
    url: '/drink/page',
    method: 'get',
    params
  })
}

// 删除接口
export const deleteDish = (ids: string) => {
  return request({
    url: '/drink',
    method: 'delete',
    params: { ids }
  })
}

// 修改接口
export const editDish = (params: any) => {
  return request({
    url: '/drink',
    method: 'put',
    data: { ...params }
  })
}

// 新增接口
export const addDish = (params: any) => {
  return request({
    url: '/drink',
    method: 'post',
    data: { ...params }
  })
}

// 查询详情
export const queryDishById = (id: string | (string | null)[]) => {
  return request({
    url: `/drink/${id}`,
    method: 'get'
  })
}

// 获取饮品分类列表
export const getCategoryList = (params: any) => {
  return request({
    url: '/category/list',
    method: 'get',
    params
  })
}

// 查饮品列表的接口
export const queryDishList = (params: any) => {
  return request({
    url: '/drink/list',
    method: 'get',
    params
  })
}

// 文件down预览
export const commonDownload = (params: any) => {
  return request({
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
    },
    url: '/common/download',
    method: 'get',
    params
  })
}

// 上架下架---批量上架下架接口
export const dishStatusByStatus = (params: any) => {
  return request({
    url: `/drink/status/${params.status}`,
    method: 'post',
    params: { id: params.id }
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
