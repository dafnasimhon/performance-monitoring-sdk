import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts'

export default function StartupChart({ data }) {
  if (!data || data.length === 0) {
    return <div className="empty">No startup data in this range.</div>
  }

  const points = data.map((d) => ({
    time: new Date(d.timestamp).toLocaleTimeString(),
    durationMs: d.durationMs,
  }))

  return (
    <div className="chart-container">
      <h2>App Startup Trend</h2>
      <ResponsiveContainer width="100%" height={260}>
        <LineChart data={points} margin={{ top: 8, right: 24, left: 0, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="time" tick={{ fontSize: 11 }} />
          <YAxis unit=" ms" tick={{ fontSize: 11 }} />
          <Tooltip formatter={(v) => [`${v} ms`, 'Startup']} />
          <Line
            type="monotone"
            dataKey="durationMs"
            stroke="#6366f1"
            dot={{ r: 4 }}
            strokeWidth={2}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}
