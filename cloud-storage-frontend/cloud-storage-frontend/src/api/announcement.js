import request from './request'

/**
 * 获取公告列表 (GET /admin/announcements)
 * 此接口供所有用户查看
 */
export const getAnnouncementList = () => {
  return request({
    url: '/admin/announcements',
    method: 'get'
  })
}