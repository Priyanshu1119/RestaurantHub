import { useState } from 'react'
import { useSelector } from 'react-redux'
import { sendChatMessage } from '../api/chatApi'

const RESTAURANT_ID = 1

export default function ChatWidget() {
  const { token } = useSelector((state) => state.auth)
  const [open, setOpen] = useState(false)
  const [sessionId, setSessionId] = useState(null)
  const [messages, setMessages] = useState([
    { role: 'assistant', content: 'Hi! How can I help you today?' }
  ])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)

  if (!token) return null

  const handleSend = async (e) => {
    e.preventDefault()
    if (!input.trim() || sending) return

    const userMessage = input.trim()
    setMessages((prev) => [...prev, { role: 'user', content: userMessage }])
    setInput('')
    setSending(true)

    try {
      const { data } = await sendChatMessage(RESTAURANT_ID, userMessage, sessionId)
      setSessionId(data.sessionId)
      setMessages((prev) => [...prev, { role: 'assistant', content: data.reply }])
    } catch (err) {
      setMessages((prev) => [...prev, {
        role: 'assistant',
        content: err.response?.data?.message || "Sorry, I couldn't process that. Please try again."
      }])
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="fixed bottom-4 right-4 z-50">
      {open && (
        <div className="w-80 h-96 bg-white rounded-lg shadow-xl mb-3 flex flex-col overflow-hidden border border-neutral-200">
          <div className="bg-brand-600 text-white px-4 py-3 flex justify-between items-center">
            <span className="font-medium text-sm">Customer Care</span>
            <button onClick={() => setOpen(false)} className="text-white text-lg leading-none">&times;</button>
          </div>
          <div className="flex-1 overflow-y-auto p-3 space-y-2">
            {messages.map((m, i) => (
              <div key={i} className={`text-sm max-w-[85%] px-3 py-2 rounded-lg ${
                m.role === 'user' ? 'bg-brand-600 text-white ml-auto' : 'bg-neutral-100 text-neutral-800'
              }`}>
                {m.content}
              </div>
            ))}
            {sending && <div className="text-xs text-neutral-400">Typing...</div>}
          </div>
          <form onSubmit={handleSend} className="border-t p-2 flex gap-2">
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Ask about menu, orders, offers..."
              className="flex-1 border border-neutral-300 rounded-md px-3 py-2 text-sm"
            />
            <button type="submit" className="bg-brand-600 text-white px-3 py-2 rounded-md text-sm">Send</button>
          </form>
        </div>
      )}
      <button
        onClick={() => setOpen(!open)}
        className="bg-brand-600 text-white w-14 h-14 rounded-full shadow-lg flex items-center justify-center text-xl"
      >
        💬
      </button>
    </div>
  )
}
