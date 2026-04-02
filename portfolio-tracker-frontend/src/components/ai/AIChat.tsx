import { useState, useRef, useEffect } from 'react';
import { Send, Bot, User } from 'lucide-react';
import { chatWithPortfolio } from '../../api/ai';
import type { ChatMessage } from '../../types';

interface Props {
  portfolioId: number;
}

const SUGGESTED = [
  'Analyze my portfolio health',
  'What are my biggest risks?',
  'Should I rebalance?',
  'Which stock should I sell?',
];

export const AIChat = ({ portfolioId }: Props) => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const send = async (text: string) => {
    if (!text.trim() || isLoading) return;
    const userMsg: ChatMessage = { role: 'user', content: text };
    setMessages((m) => [...m, userMsg]);
    setInput('');
    setIsLoading(true);

    const history = messages.slice(-10);
    let assistantContent = '';
    setMessages((m) => [...m, { role: 'assistant', content: '' }]);

    const showError = (msg: string) => {
      setMessages((m) => {
        const updated = [...m];
        updated[updated.length - 1] = { role: 'assistant', content: `⚠️ ${msg}` };
        return updated;
      });
      setIsLoading(false);
    };

    try {
      await chatWithPortfolio(
        portfolioId,
        text,
        history,
        (token) => {
          assistantContent += token;
          setMessages((m) => {
            const updated = [...m];
            updated[updated.length - 1] = { role: 'assistant', content: assistantContent };
            return updated;
          });
        },
        () => setIsLoading(false),
        showError
      );
    } catch {
      showError('Something went wrong. Please try again.');
    }
  };

  return (
    <div className="flex flex-col h-full bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">
      <div className="px-6 py-4 border-b border-slate-800 flex items-center gap-2">
        <Bot size={20} className="text-blue-400" />
        <h2 className="text-white font-semibold">Portfolio AI Assistant</h2>
      </div>

      <div className="flex-1 overflow-y-auto p-6 space-y-4 min-h-0">
        {messages.length === 0 && (
          <div className="text-center py-8">
            <Bot size={40} className="mx-auto text-slate-600 mb-3" />
            <p className="text-slate-500 mb-6">Ask me anything about your portfolio</p>
            <div className="flex flex-wrap gap-2 justify-center">
              {SUGGESTED.map((q) => (
                <button
                  key={q}
                  onClick={() => send(q)}
                  className="px-3 py-1.5 bg-slate-800 text-slate-300 rounded-full text-sm hover:bg-slate-700 hover:text-white transition-colors"
                >
                  {q}
                </button>
              ))}
            </div>
          </div>
        )}

        {messages.map((msg, i) => (
          <div key={i} className={`flex gap-3 ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            {msg.role === 'assistant' && (
              <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center flex-shrink-0 mt-0.5">
                <Bot size={16} className="text-white" />
              </div>
            )}
            <div
              className={`max-w-[75%] rounded-2xl px-4 py-3 text-sm leading-relaxed ${
                msg.role === 'user'
                  ? 'bg-blue-600 text-white rounded-br-sm'
                  : 'bg-slate-800 text-slate-200 rounded-bl-sm'
              }`}
            >
              {msg.content || (isLoading && i === messages.length - 1 ? (
                <span className="flex gap-1">
                  <span className="w-1.5 h-1.5 bg-slate-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
                  <span className="w-1.5 h-1.5 bg-slate-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
                  <span className="w-1.5 h-1.5 bg-slate-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
                </span>
              ) : '')}
            </div>
            {msg.role === 'user' && (
              <div className="w-8 h-8 rounded-full bg-slate-700 flex items-center justify-center flex-shrink-0 mt-0.5">
                <User size={16} className="text-white" />
              </div>
            )}
          </div>
        ))}
        <div ref={bottomRef} />
      </div>

      <div className="p-4 border-t border-slate-800">
        <form
          className="flex gap-2"
          onSubmit={(e) => { e.preventDefault(); send(input); }}
        >
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask about your portfolio..."
            disabled={isLoading}
            className="flex-1 bg-slate-800 border border-slate-700 rounded-lg px-4 py-2.5 text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 text-sm"
          />
          <button
            type="submit"
            disabled={isLoading || !input.trim()}
            className="bg-blue-600 hover:bg-blue-500 disabled:opacity-50 disabled:cursor-not-allowed text-white rounded-lg px-4 py-2.5 transition-colors"
          >
            <Send size={18} />
          </button>
        </form>
      </div>
    </div>
  );
};
