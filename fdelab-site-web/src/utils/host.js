export function talentSlugFromHost(hostname) {
  const host = hostname.split(':')[0].toLowerCase()
  if (host.endsWith('.localhost')) {
    return host.slice(0, -'.localhost'.length)
  }
  if (host.endsWith('.fdelab.io')) {
    const slug = host.slice(0, -'.fdelab.io'.length)
    if (slug && slug !== 'www') return slug
  }
  return ''
}

export function visitorKey() {
  const existing = localStorage.getItem('fdelab_visitor')
  if (existing) return existing
  const created = crypto.randomUUID()
  localStorage.setItem('fdelab_visitor', created)
  return created
}
