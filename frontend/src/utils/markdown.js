import { marked } from 'marked'
import { markedHighlight } from 'marked-highlight'
import hljs from 'highlight.js'
import DOMPurify from 'dompurify'

marked.use(
  markedHighlight({
    langPrefix: 'hljs language-',
    highlight(code, lang) {
      const language = hljs.getLanguage(lang) ? lang : 'plaintext'
      return hljs.highlight(code, { language }).value
    },
  })
)

function makeHeadingId(text) {
  return text
    .replace(/<[^>]*>/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9가-힣\s]/g, '')
    .replace(/\s+/g, '-')
    .trim()
}

marked.use({
  breaks: true,
  gfm: true,
  renderer: {
    heading({ text, depth }) {
      const id = makeHeadingId(text)
      return `<h${depth} id="${id}">${text}</h${depth}>\n`
    },
  },
})

const PURIFY_CONFIG = {
  ADD_ATTR: ['class', 'id'],
  ALLOWED_URI_REGEXP:
    /^(?:(?:(?:f|ht)tps?|mailto|tel|callto|sms|cid|xmpp|blob):|data:image\/|[^a-z]|[a-z+.\-]+(?:[^a-z+.\-:]|$))/i,
}

export function renderMarkdown(content) {
  if (!content) return ''
  const html = marked(content)
  return DOMPurify.sanitize(html, PURIFY_CONFIG)
}

export function extractToc(content) {
  if (!content) return []
  const headings = []
  for (const line of content.split('\n')) {
    const match = line.match(/^(#{1,3})\s+(.+)$/)
    if (match) {
      const level = match[1].length
      const text = match[2].trim()
      const id = makeHeadingId(text)
      headings.push({ level, text, id })
    }
  }
  return headings
}
