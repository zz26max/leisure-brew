import md5 from 'md5'

const pending: Record<string, boolean> = {}

const getRequestKey = (config: any) => {
  const data = typeof config.data === 'string' ? config.data : JSON.stringify(config.data || {})
  const params = JSON.stringify(config.params || {})
  return md5(`${config.method || 'get'}&${config.url || ''}&${params}&${data}`)
}

const checkPending = (key: string) => Boolean(pending[key])
const removePending = (key: string) => delete pending[key]

export { getRequestKey, pending, checkPending, removePending }
