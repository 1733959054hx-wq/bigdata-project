// src/components/collaboration/CollaborationService.ts
import { QuillBinding } from 'y-quill';
import { WebsocketProvider } from 'y-websocket';
import * as Y from 'yjs';

export class CollaborationService {
  wsUrl: string;
  
  constructor(wsUrl: string) {
    this.wsUrl = wsUrl;
  }

  setupCollaboration(docId: string, quill: any, userInfo: any) {
    const ydoc = new Y.Doc();
    const provider = new WebsocketProvider(this.wsUrl, docId, ydoc);
    const ytext = ydoc.getText('quill');
    
    // 绑定 Quill 和 Yjs
    const binding = new QuillBinding(ytext, quill, provider.awareness);
    
    // 设置用户信息
    provider.awareness.setLocalStateField('user', {
      name: userInfo.name,
      color: userInfo.color,
      avatar: userInfo.avatar
    });

    // 事件监听
    provider.on('status', (event: any) => {
      console.log('Yjs 连接状态:', event.status);
    });

    provider.on('sync', (isSynced: boolean) => {
      console.log('Yjs 同步状态:', isSynced);
    });

    provider.on('connection-close', (event: any) => {
      console.log('Yjs 连接关闭:', event);
    });

    provider.on('connection-error', (event: any) => {
      console.error('Yjs 连接错误:', event);
    });

    // 监听 awareness 变化
    provider.awareness.on('change', () => {
      console.log('用户状态变化');
    });

    return { ydoc, provider, binding };
  }

  // 销毁资源
  destroyCollaboration(provider?: WebsocketProvider, binding?: QuillBinding, ydoc?: Y.Doc) {
    if (binding) {
      binding.destroy();
    }
    if (provider) {
      provider.destroy();
    }
    if (ydoc) {
      ydoc.destroy();
    }
  }
}