// src/components/collaboration/QuillEditor.ts
import Quill from 'quill';
import QuillCursors from 'quill-cursors';
import 'quill/dist/quill.snow.css';

// 注册光标模块
Quill.register('modules/cursors', QuillCursors);

export class QuillEditor {
  quillInstance: Quill;
  
  constructor(editorContainer: HTMLElement) {
    this.quillInstance = new Quill(editorContainer, {
      theme: 'snow',
      modules: {
        toolbar: [
          [{ 'header': [1, 2, 3, false] }],
          ['bold', 'italic', 'underline', 'strike'],
          [{ 'color': [] }, { 'background': [] }],
          [{ 'script': 'sub'}, { 'script': 'super' }],
          [{ 'list': 'ordered'}, { 'list': 'bullet' }],
          [{ 'indent': '-1'}, { 'indent': '+1' }],
          [{ 'direction': 'rtl' }],
          [{ 'size': ['small', false, 'large', 'huge'] }],
          [{ 'font': [] }],
          [{ 'align': [] }],
          ['blockquote', 'code-block'],
          ['link', 'image', 'video'],
          ['clean']
        ],
        cursors: {
          transformOnTextChange: true,
          selectionChangeSource: null,
          hideDelayMs: 3000,
          hideSpeedMs: 300,
          // 光标配置
          template: '<div class="custom-cursor"></div>'
        },
        history: {
          delay: 1000,
          maxStack: 500,
          userOnly: true
        }
      },
      placeholder: '开始协同编辑...',
      formats: [
        'header', 'font', 'size',
        'bold', 'italic', 'underline', 'strike', 'blockquote',
        'list', 'bullet', 'indent',
        'link', 'image', 'video',
        'color', 'background',
        'code-block', 'script', 'align', 'direction'
      ]
    });

    // 添加自定义光标样式
    this.addCustomCursorStyles();
  }

  getInstance(): Quill {
    return this.quillInstance;
  }

  // 添加自定义光标样式
  private addCustomCursorStyles(): void {
    const style = document.createElement('style');
    style.textContent = `
      .custom-cursor {
        border-left: 2px solid;
        margin-left: -1px;
        position: relative;
      }
      
      .custom-cursor::before {
        content: '';
        position: absolute;
        left: -4px;
        top: 0;
        width: 8px;
        height: 100%;
        background: currentColor;
        opacity: 0.2;
      }
      
      .custom-cursor::after {
        content: attr(data-username);
        position: absolute;
        left: -2px;
        top: -20px;
        background: currentColor;
        color: white;
        padding: 2px 6px;
        border-radius: 3px;
        font-size: 12px;
        white-space: nowrap;
        pointer-events: none;
      }
      
      .ql-editor .ql-cursor {
        pointer-events: none;
      }
      
      .ql-editor .ql-cursor .ql-cursor-caret {
        margin-left: -1px;
      }
    `;
    document.head.appendChild(style);
  }

  destroy(): void {
    // 清理资源
    if (this.quillInstance) {
      this.quillInstance = null as any;
    }
  }
}