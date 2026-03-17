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
  ADD_ATTR: ['class', 'id', 'data-bookmark-url', 'target', 'rel'],
  ALLOWED_URI_REGEXP:
    /^(?:(?:(?:f|ht)tps?|mailto|tel|callto|sms|cid|xmpp|blob):|data:image\/|[^a-z]|[a-z+.\-]+(?:[^a-z+.\-:]|$))/i,
}

// :::bookmark\nhttps://...\n::: → 북마크 카드 HTML로 변환
function preprocessBookmarks(content) {
  return content.replace(
    /:::bookmark\n(https?:\/\/[^\n]+)\n:::/g,
    (_, url) => {
      let hostname = url
      try { hostname = new URL(url).hostname } catch {}
      const safe = url.replace(/"/g, '&quot;')
      return (
        `<div class="bookmark-card" data-bookmark-url="${safe}">` +
          `<a href="${safe}" target="_blank" rel="noopener noreferrer">` +
            `<div class="bookmark-card-body">` +
              `<div class="bookmark-card-text">` +
                `<div class="bm-title">${hostname}</div>` +
                `<div class="bm-description"></div>` +
                `<div class="bm-url-row">` +
                  `<img class="bm-favicon" src="" alt="" />` +
                  `<span class="bm-hostname">${hostname}</span>` +
                `</div>` +
              `</div>` +
              `<div class="bm-thumbnail-wrapper">` +
                `<img class="bm-image" src="" alt="" />` +
              `</div>` +
            `</div>` +
          `</a>` +
        `</div>`
      )
    }
  )
}

export function renderMarkdown(content) {
  if (!content) return ''
  const processed = preprocessBookmarks(content)
  const html = marked(processed)
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
