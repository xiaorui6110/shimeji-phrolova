import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'

export default withMermaid({
  title: 'Shimeji-Phrolova',
  description: '一个桌面吉祥物应用程序，让可爱的角色在您的屏幕上自由活动',
  lang: 'zh-CN',

  // 忽略死链接检查，内容仍在逐步完善中
  ignoreDeadLinks: true,

  // 仓库说明文件不构建为站点页面
  srcExclude: ['README.md'],

  vite: {
    optimizeDeps: {
      // fastdom 及其扩展都是老式 CJS 库且无 ESM 导出，dev 模式下需显式预构建，
      // 否则 mermaid 的 `import fastdom from 'fastdom'` 与
      // `import fastdomPromised from 'fastdom/extensions/fastdom-promised.js'`
      // 会因缺少 default 导出而报错（preview/构建走 Rollup，无此问题）
      include: ['fastdom', 'fastdom/extensions/fastdom-promised.js']
    }
  },

  themeConfig: {
    nav: [
      { text: '首页', link: '/' },
      { text: '用户指南', link: '/user/install' },
      { text: '开发文档', link: '/development/getting-started' },
      { text: 'GitHub', link: 'https://github.com/xiaorui6110/shimeji-phrolova' }
    ],

    sidebar: {
      '/user/': [
        {
          text: '用户指南',
          items: [
            { text: '安装与运行', link: '/user/install' }
          ]
        }
      ],
      '/development/': [
        {
          text: '开发文档',
          items: [
            { text: '快速开始', link: '/development/getting-started' },
            { text: '架构总览', link: '/development/architecture' },
            { text: '配置系统', link: '/development/configuration' },
            { text: '打包与分发', link: '/development/packaging' }
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/xiaorui6110/shimeji-phrolova' }
    ],

    footer: {
      message: '基于 Shimeji 与 Shimeji-ee 的开源实现，沿用 zlib 与 BSD-2-Clause 许可',
      copyright: 'Shimeji-Phrolova'
    },

    search: {
      provider: 'local',
      options: {
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
    },

    editLink: {
      pattern: 'https://github.com/xiaorui6110/shimeji-phrolova/edit/main/docs-site/:path'
    },

    lastUpdated: {
      text: '最后更新于',
      formatOptions: {
        dateStyle: 'short',
        timeStyle: 'medium'
      }
    }
  }
})
