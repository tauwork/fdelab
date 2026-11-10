/**
 * 指标行下标重映射。
 *
 * kpis[].m 是 0-based 的 metrics 下标。指标行删除或排序后，必须同步改写所有 KPI 的 m：
 * - 删除：原下标等于被删行的 KPI 置空；大于被删行的减一。
 * - 排序：按旧下标到新下标的映射改写。
 */

export function removeMetricAt(metrics, kpis, index) {
  if (index < 0 || index >= metrics.length) return
  metrics.splice(index, 1)
  for (const kpi of kpis || []) {
    if (kpi.m === index) {
      kpi.m = null
    } else if (typeof kpi.m === 'number' && kpi.m > index) {
      kpi.m -= 1
    }
  }
}

export function moveMetric(metrics, kpis, from, to) {
  if (from < 0 || from >= metrics.length || to < 0 || to >= metrics.length || from === to) return
  const [row] = metrics.splice(from, 1)
  metrics.splice(to, 0, row)

  const mapIndex = (oldIndex) => {
    if (typeof oldIndex !== 'number') return oldIndex
    if (oldIndex === from) return to
    if (from < to && oldIndex > from && oldIndex <= to) return oldIndex - 1
    if (from > to && oldIndex >= to && oldIndex < from) return oldIndex + 1
    return oldIndex
  }

  for (const kpi of kpis || []) {
    kpi.m = mapIndex(kpi.m)
  }
}
