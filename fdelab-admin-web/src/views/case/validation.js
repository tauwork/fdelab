/**
 * 前端校验，规则与后端 CaseAdminService.validate 保持一致。
 * section 与编辑页 9 个锚点 id 一致。
 */

const CRED = ['A', 'B', 'C', 'D']

export function hasText(value) {
  return value != null && String(value).trim() !== ''
}

export function splitCredNote(note) {
  const match = /^(.*?)（(.*?)）$/.exec(String(note || ''))
  if (!match) return [String(note || ''), '']
  return [match[1], match[2]]
}

export function joinCredNote(main, sub) {
  const title = String(main || '').trim()
  const subtitle = String(sub || '').trim()
  if (!title) return subtitle
  return subtitle ? `${title}（${subtitle}）` : title
}

function issue(section, field, message) {
  return { section, field, message }
}

function rows(list) {
  return (list || []).filter(Boolean)
}

export function isMetricBlank(row) {
  return !hasText(row.name) && !hasText(row.definition) && !hasText(row.baseline)
    && !hasText(row.result) && !hasText(row.window) && !hasText(row.cred)
    && (row.sourceRef == null || row.sourceRef === '')
}

export function isSourceBlank(row) {
  return !hasText(row.title) && !hasText(row.url) && !hasText(row.type)
}

function isTestimonyBlank(row) {
  return !hasText(row.quote) && !hasText(row.attribution)
}

function decimalPlaces(value) {
  const text = String(value)
  if (!text.includes('.')) return 0
  return text.split('.')[1].length
}

export function validateForm(form) {
  const errors = []
  const warnings = []
  if (!form) {
    return { ok: false, errors: [issue('basic', 'form', '表单不能为空')], warnings }
  }

  const rawSources = Array.isArray(form.sources) ? form.sources : []
  const metrics = rows(form.metrics).filter((row) => !isMetricBlank(row))
  const sources = rawSources.filter((row) => row && !isSourceBlank(row))
  const testimony = rows(form.testimony).filter((row) => !isTestimonyBlank(row))

  const title = hasText(form.title) ? String(form.title).trim() : ''
  if (!title) {
    errors.push(issue('basic', 'title', '标题不能为空'))
  } else if (title.length > 200) {
    errors.push(issue('basic', 'title', '标题不能超过 200 字'))
  } else if (title.length > 40) {
    warnings.push(issue('basic', 'title', '标题超过 40 字，列表卡片会截断'))
  }
  if (!hasText(form.industry)) {
    errors.push(issue('basic', 'industry', '行业不能为空'))
  }
  const hasCustomer = form.customerId != null || hasText(form.newCustomer?.displayName)
  if (!hasCustomer) {
    errors.push(issue('basic', 'customerId', '请选择客户或在案例内新建客户'))
  }
  if (form.modelPct != null && form.modelPct !== '') {
    const value = Number(form.modelPct)
    if (!Number.isFinite(value) || value < 0 || value > 100) {
      errors.push(issue('basic', 'modelPct', '模型含量需在 0–100 之间'))
    }
    if (decimalPlaces(form.modelPct) > 1) {
      errors.push(issue('basic', 'modelPct', '模型含量最多保留一位小数'))
    }
  }

  const cred = hasText(form.cred) ? String(form.cred).trim().toUpperCase() : ''
  if (!CRED.includes(cred)) {
    errors.push(issue('credibility', 'cred', '可信度需为 A/B/C/D'))
  }

  if (!hasText(form.pain)) errors.push(issue('narrative', 'pain', '痛点不能为空'))
  if (!hasText(form.goal)) errors.push(issue('narrative', 'goal', '目标不能为空'))

  if (metrics.length === 0) {
    errors.push(issue('effects', 'metrics', '至少需要一条指标'))
  }
  metrics.forEach((row, index) => {
    const no = index + 1
    if (!hasText(row.name)) {
      errors.push(issue('effects', `metrics[${index}].name`, `第 ${no} 条指标缺少指标名`))
    }
    const metricCred = hasText(row.cred) ? String(row.cred).trim().toUpperCase() : ''
    if (!CRED.includes(metricCred)) {
      errors.push(issue('effects', `metrics[${index}].cred`, `第 ${no} 条指标可信度需为 A/B/C/D`))
    }
    const sourceRef = row.sourceRef == null || row.sourceRef === '' ? null : Number(row.sourceRef)
    if (sourceRef != null) {
      const referenced = rawSources[sourceRef - 1]
      if (!Number.isInteger(sourceRef) || sourceRef < 1 || !referenced || isSourceBlank(referenced)) {
        errors.push(issue('effects', `metrics[${index}].sourceRef`, `第 ${no} 条指标的来源序号超出范围`))
      }
    }
    if (!hasText(row.baseline)) {
      warnings.push(issue('effects', `metrics[${index}].baseline`, `第 ${no} 条指标缺少基线`))
    }
    if (!hasText(row.window)) {
      warnings.push(issue('effects', `metrics[${index}].window`, `第 ${no} 条指标缺少时间窗`))
    }
  })

  rows(form.kpis).forEach((kpi, index) => {
    if (kpi.m == null) {
      warnings.push(issue('effects', `kpis[${index}].m`, `第 ${index + 1} 个 KPI 未关联指标行`))
    }
  })

  if (sources.length === 0) {
    errors.push(issue('sources', 'sources', '至少需要一条来源'))
  }
  sources.forEach((row, index) => {
    const url = String(row.url || '').trim()
    if (!url.startsWith('http://') && !url.startsWith('https://')) {
      errors.push(issue('sources', `sources[${index}].url`, `第 ${index + 1} 条来源的 URL 需以 http:// 或 https:// 开头`))
    }
  })

  if (cred === 'A' || cred === 'B') {
    const hasTestimony = testimony.some((row) => hasText(row.quote))
    const hasOfficialSource = sources.some((row) => {
      const type = String(row.type || '')
      return type.includes('一手') || type.includes('官方')
    })
    if (!hasTestimony && !hasOfficialSource) {
      errors.push(issue('sources', 'testimony', '可信度 A/B 级需至少一条证言，或一条类型含「一手」「官方」的来源'))
    }
  }

  addDomainWarnings(sources, form.partner, warnings)
  return { ok: errors.length === 0, errors, warnings }
}

function addDomainWarnings(sources, partner, warnings) {
  const text = String(partner || '')
  const tokens = (text.match(/[A-Za-z]{3,}/g) || []).map((item) => item.toLowerCase())
  if (tokens.length === 0) return
  sources.forEach((row, index) => {
    const type = String(row.type || '')
    if (type.includes('一手') || type.includes('官方')) return
    let host = ''
    try {
      host = new URL(String(row.url || '')).hostname.toLowerCase()
    } catch {
      return
    }
    if (!host) return
    if (!tokens.some((token) => host.includes(token))) {
      warnings.push(issue('sources', `sources[${index}].url`, `第 ${index + 1} 条来源域名与交付方可能不匹配`))
    }
  })
}

/** 按 section 汇总圆点状态：error > warning > filled。 */
export function sectionStatus(validation, section, filled = true) {
  if (validation?.errors?.some((item) => item.section === section)) return 'error'
  if (validation?.warnings?.some((item) => item.section === section)) return 'warning'
  return filled ? 'filled' : 'empty'
}
