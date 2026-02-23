import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Message } from '../models/message.model';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private apiUrl = 'http://localhost:8080/api';
  private stompClient: Client | null = null;
  private messageSubject = new Subject<Message>();

  constructor(private http: HttpClient) {}

  connect(conversationId: number, token: string): void {
    // évite les abonnements doublons si l'agent passe d'une conversation à l'autre
    this.disconnect();

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      // gestion des erreurs de connexion WebSocket
      onStompError: (frame) => {
        console.error('Erreur STOMP :', frame.headers['message']);
      },
      onWebSocketError: (error) => {
        console.error('Erreur WebSocket :', error);
      },
      onConnect: () => {
        this.stompClient!.subscribe(`/topic/conversation.${conversationId}`, (frame: IMessage) => {
          try {
            const message: Message = JSON.parse(frame.body);
            this.messageSubject.next(message);
          } catch (e) {
            console.error('Erreur parsing message WebSocket :', e);
          }
        });
      },
    });

    this.stompClient.activate();
  }

  disconnect(): void {
    if (this.stompClient?.active) {
      this.stompClient.deactivate();
    }
    this.stompClient = null;
  }

  sendMessage(conversationId: number, message: string): void {
    if (!this.stompClient?.active) {
      console.warn('WebSocket non connecté, message non envoyé');
      return;
    }
    this.stompClient.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ conversationId, message }),
    });
  }

  getMessages(): Observable<Message> {
    return this.messageSubject.asObservable();
  }

  getOrCreateConversation(customerId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/conversations?customerId=${customerId}`, {});
  }

  getHistory(conversationId: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}/conversations/${conversationId}/history`);
  }

  getPendingConversations(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/conversations/pending`);
  }

  getMyConversations(agentId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/conversations/my?agentId=${agentId}`);
  }

  assignAgent(conversationId: number, agentId: number): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/conversations/${conversationId}/assign?agentId=${agentId}`,
      {},
    );
  }
}
