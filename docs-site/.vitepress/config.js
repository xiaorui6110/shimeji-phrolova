import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'Shimeji-Live',
  description: '一个桌面吉祥物应用程序，让可爱的角色在您的屏幕上自由活动',
  lang: 'zh-CN',
  
  // 忽略死链接检查，在开发阶段很有用
  ignoreDeadLinks: true,
  
  head: [
    ['link', { rel: 'icon', href: '/icon.ico' }],
    ['link', { rel: 'icon', type: 'image/png', href: '/icon.png' }],
    ['meta', { name: 'theme-color', content: '#3c82f6' }]
  ],

  locales: {
    root: {
      label: '简体中文',
      lang: 'zh-CN',
      title: 'Shimeji-Live',
      description: '桌面吉祥物应用程序文档'
    },
    en: {
      label: 'English',
      lang: 'en-US',
      title: 'Shimeji-Live',
      description: 'Desktop mascot application documentation'
    }
  },

  themeConfig: {
    logo: '/icon.png',
    
    nav: [
      { text: '首页', link: '/' },
      { text: '用户指南', link: '/user/install' },
      { text: '开发文档', link: '/development/getting-started' },
      { text: 'GitHub', link: 'https://github.com/DCRepairCenter/ShimejiLive' }
    ],

    sidebar: {
      '/user/': [
        {
          text: '用户指南',
          items: [
            { text: '安装指南', link: '/user/install' },
            { text: '教程目录', link: '/user/tutorial/' },
            {
              text: '进阶教程',
              collapsed: false,
              items: [
                { text: '入门介绍', link: '/user/tutorial/introduction' },
                { text: '动作基础', link: '/user/tutorial/actions-foundation' },
                { text: '行为系统', link: '/user/tutorial/behaviors-brain' },
                { text: '脚本引擎', link: '/user/tutorial/scripting-engine' },
                { text: '个性化配置', link: '/user/tutorial/personality-mods' },
                { text: '高级互动', link: '/user/tutorial/advanced-interaction' },
                { text: '逐帧解析', link: '/user/tutorial/frame-analysis' }
              ]
            }
          ]
        }
      ],
      '/development/': [
        {
          text: '开发文档',
          items: [
            { text: '开发指南', link: '/development/getting-started' },
            { text: '开发技巧', link: '/development/tips' }
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/BegoniaHe/dc-ShimejiLive' }
    ],

    footer: {
      message: '基于 zlib License 发布',
      copyright: '版权所有 © 2009-2025 Group Finity & Shimeji-ee Group'
    },

    search: {
      provider: 'local',
      options: {
        locales: {
          zh: {
            translations: {
              button: {
                buttonText: '搜索文档',
                buttonAriaLabel: '搜索文档'
              },
              modal: {
                noResultsText: '无法找到相关结果',
                resetButtonTitle: '清除查询条件',
                footer: {
                  selectText: '选择',
                  navigateText: '切换'
                }
              }
            }
          }
        }
      }
    },

    editLink: {
      pattern: 'https://github.com/DCRepairCenter/ShimejiLive/edit/main/docs-site/:path'
    },

    lastUpdated: {
      text: '最后更新于',
      formatOptions: {
        dateStyle: 'short',
        timeStyle: 'medium'
      }
    }
  },

  vite: {
    publicDir: '../img'
  }
})
