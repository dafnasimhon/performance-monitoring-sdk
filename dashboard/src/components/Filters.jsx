export default function Filters({ filters, onChange }) {
  const set = (key, val) => onChange({ ...filters, [key]: val })

  const ranges = [
    { label: 'Last 24h', hours: 24 },
    { label: 'Last 7d',  hours: 24 * 7 },
    { label: 'Last 30d', hours: 24 * 30 },
  ]

  return (
    <div className="filters">
      <label>
        Time range
        <select
          value={filters.hours}
          onChange={(e) => set('hours', Number(e.target.value))}
        >
          {ranges.map((r) => (
            <option key={r.hours} value={r.hours}>{r.label}</option>
          ))}
        </select>
      </label>

      <label>
        App version
        <input
          placeholder="e.g. 1.0"
          value={filters.appVersion}
          onChange={(e) => set('appVersion', e.target.value)}
        />
      </label>

      <label>
        Network
        <select
          value={filters.networkType}
          onChange={(e) => set('networkType', e.target.value)}
        >
          <option value="">All</option>
          <option value="WIFI">WIFI</option>
          <option value="CELLULAR">CELLULAR</option>
        </select>
      </label>

      <label>
        Device model
        <input
          placeholder="e.g. Pixel 8"
          value={filters.deviceModel}
          onChange={(e) => set('deviceModel', e.target.value)}
        />
      </label>
    </div>
  )
}
