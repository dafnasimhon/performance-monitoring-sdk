import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'

export default function EventsOverTime({ data }) {
  if (!data?.length) return (
    <div className="card">
      <h2>Events Over Time</h2>
      <p className="empty">No events in this time range.</p>
    </div>
  )

  const formatted = data.map(d => ({
    hour: new Date(d.hourTs).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    count: d.count,
  }))

  return (
    <div className="card">
      <h2>Events Over Time</h2>
      <ResponsiveContainer width="100%" height={200}>
        <BarChart data={formatted} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
          <CartesianGrid strokeDasharray="3 3" vertical={false} />
          <XAxis dataKey="hour" tick={{ fontSize: 11 }} interval="preserveStartEnd" />
          <YAxis allowDecimals={false} tick={{ fontSize: 11 }} width={30} />
          <Tooltip formatter={(v) => [v, 'Events']} />
          <Bar dataKey="count" fill="#6366F1" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}
