export function visitorKey() {
  const existing = uni.getStorageSync('fdelab_visitor')
  if (existing) return existing
  const created = `${Date.now().toString(36)}${Math.random().toString(36).slice(2, 10)}`
  uni.setStorageSync('fdelab_visitor', created)
  return created
}
