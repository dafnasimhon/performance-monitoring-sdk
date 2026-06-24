import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'PerfSDK',
  description: 'Android Performance Monitoring SDK — automatic startup, screen, network, and custom trace measurement.',
  head: [['link', { rel: 'icon', href: '/images/logo.png' }]],

  themeConfig: {
    logo: '/images/logo.png',
    siteTitle: 'PerfSDK',

    nav: [
      { text: 'Home',          link: '/' },
      { text: 'Get Started',   link: '/getting-started' },
      { text: 'API Reference', link: '/api-reference' },
      { text: 'Dashboard',     link: '/dashboard' },
    ],

    sidebar: [
      {
        text: 'Introduction',
        items: [
          { text: 'Who It\'s For',   link: '/about' },
          { text: 'Get Started',     link: '/getting-started' },
        ],
      },
      {
        text: 'Guide',
        items: [
          { text: 'How It Works',  link: '/how-it-works' },
          { text: 'How to Use',    link: '/how-to-use' },
          { text: 'Examples',      link: '/examples' },
        ],
      },
      {
        text: 'Reference',
        items: [
          { text: 'API Reference', link: '/api-reference' },
          { text: 'Dashboard',     link: '/dashboard' },
        ],
      },
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/dafnasimhon/perfsdk' },
    ],

    footer: {
      message: 'Built as a seminar project at Afeka College of Engineering.',
    },

    search: { provider: 'local' },
  },
})
