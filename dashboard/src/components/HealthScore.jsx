const GRADE_COLOR = { A: '#16a34a', B: '#65a30d', C: '#d97706', D: '#ea580c', F: '#dc2626', 'N/A': '#9ca3af' }

const COMPONENT_LABEL = {
  startup:   'App Startup',
  screen:    'Screen Load',
  network:   'Network Speed',
  errorRate: 'Error Rate',
}

const MAX_PTS = 25

export default function HealthScore({ data }) {
  if (!data) return null

  const { score, grade, components, details } = data
  const color = GRADE_COLOR[grade] ?? '#9ca3af'

  return (
    <div className="health-card">
      <div className="health-score-circle" style={{ borderColor: color }}>
        <span className="health-score-number" style={{ color }}>{score ?? '—'}</span>
        <span className="health-score-label">/ 100</span>
        <span className="health-grade" style={{ color }}>{grade}</span>
      </div>

      <div className="health-breakdown">
        <h2>App Health Score</h2>
        <p className="health-subtitle">
          Based on startup time, screen load, network latency, and error rate.
        </p>
        <div className="health-bars">
          {Object.entries(components).map(([key, pts]) => {
            const pct = pts == null ? 0 : (pts / MAX_PTS) * 100
            const label = COMPONENT_LABEL[key]
            const detail = formatDetail(key, details)
            return (
              <div key={key} className="health-bar-row">
                <div className="health-bar-meta">
                  <span className="health-bar-name">{label}</span>
                  <span className="health-bar-detail">{detail}</span>
                  <span className="health-bar-pts" style={{ color: ptsColor(pts) }}>
                    {pts == null ? 'N/A' : `${pts} / ${MAX_PTS}`}
                  </span>
                </div>
                <div className="health-bar-bg">
                  <div
                    className="health-bar-fill"
                    style={{ width: `${pct}%`, background: ptsColor(pts) }}
                  />
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}

function formatDetail(key, details) {
  if (!details) return ''
  if (key === 'startup')   return details.avgStartupMs != null ? `avg ${details.avgStartupMs} ms` : '—'
  if (key === 'screen')    return details.avgScreenMs  != null ? `avg ${details.avgScreenMs} ms`  : '—'
  if (key === 'network')   return details.avgNetworkMs != null ? `avg ${details.avgNetworkMs} ms` : '—'
  if (key === 'errorRate') return `${details.errorRate}% errors`
  return ''
}

function ptsColor(pts) {
  if (pts == null)    return '#9ca3af'
  if (pts >= 20)      return '#16a34a'
  if (pts >= 10)      return '#d97706'
  return '#dc2626'
}
