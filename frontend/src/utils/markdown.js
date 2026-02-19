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

marked.use({ breaks: true, gfm: true })

// blob: (편집 중 이미지 미리보기) 와 data:image/ (저장된 이미지) 를 허용
const PURIFY_CONFIG = {
  ADD_ATTR: ['class'],
  ALLOWED_URI_REGEXP:
    /^(?:(?:(?:f|ht)tps?|mailto|tel|callto|sms|cid|xmpp|blob):|data:image\/|[^a-z]|[a-z+.\-]+(?:[^a-z+.\-:]|$))/i,
}

export function renderMarkdown(content) {
  if (!content) return ''
  const html = marked(content)
  return DOMPurify.sanitize(html, PURIFY_CONFIG)
}
