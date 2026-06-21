export default function KpiCard({ label, value, unit = 'ms' }) {
  const display = value == null ? '—' : `${Math.round(value)} ${unit}`
  return (
    <div className="kpi-card">
      <div className="kpi-value">{display}</div>
      <div className="kpi-label">{label}</div>
    </div>
  )
}
