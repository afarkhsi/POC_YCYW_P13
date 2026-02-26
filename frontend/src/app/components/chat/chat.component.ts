import {
  Component,
  OnInit,
  OnDestroy,
  ViewChild,
  ElementRef,
  AfterViewChecked,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { Message } from '../../models/message.model';
import { User } from '../../models/user.model';
import { AuthService } from '../../services/auth.service';
import { ChatService } from '../../services/chat.service';
import { switchMap, takeWhile } from 'rxjs/operators';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css'],
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  currentUser: User | null = null;
  conversationId: number | null = null;
  messages: Message[] = [];
  newMessage = '';
  pendingConversations: any[] = [];
  myConversations: any[] = [];
  // états de chargement et d'erreur
  isConnecting = false;
  errorMessage = '';

  // référence à la zone de messages pour l'auto-scroll
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;
  private shouldScrollToBottom = false;

  private messageSubscription: Subscription | null = null;
  private pollingSubscription: Subscription | null = null;

  constructor(
    private authService: AuthService,
    private chatService: ChatService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    if (!this.currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    if (this.currentUser.type === 'CLIENT') {
      this.initClientChat();
    } else {
      this.loadPendingConversations();
      this.startPolling();
    }
  }

  // auto-scroll après chaque mise à jour de la vue
  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  private scrollToBottom(): void {
    try {
      const el = this.messagesContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch (e) {}
  }

  // Rafraîchissement automatique des listes toutes les 5 secondes
  // S'arrête automatiquement dès que l'agent rejoint une conversation
  private startPolling(): void {
    this.pollingSubscription = interval(5000)
      .pipe(
        // takeWhile arrête le polling quand l'agent est dans une conversation
        takeWhile(() => this.conversationId === null),
        switchMap(
          () =>
            new Promise<void>((resolve) => {
              this.chatService.getPendingConversations().subscribe({
                next: (convs) => (this.pendingConversations = convs),
                error: (err) => console.error('Polling pending error:', err),
              });
              this.chatService.getMyConversations(this.currentUser!.userId).subscribe({
                next: (convs) => {
                  this.myConversations = convs;
                  resolve();
                },
                error: (err) => {
                  console.error('Polling my conversations error:', err);
                  resolve();
                },
              });
            }),
        ),
      )
      .subscribe();
  }

  // arrêt explicite du polling (appelé quand l'agent rejoint une conversation)
  private stopPolling(): void {
    this.pollingSubscription?.unsubscribe();
    this.pollingSubscription = null;
  }

  initClientChat(): void {
    this.isConnecting = true;
    this.chatService.getOrCreateConversation(this.currentUser!.userId).subscribe({
      next: (conversation) => {
        this.conversationId = conversation.id;
        this.loadHistoryAndConnect();
      },
      error: (err) => {
        this.isConnecting = false;
        this.errorMessage = 'Impossible de démarrer la conversation. Réessayez.';
        console.error('Erreur getOrCreateConversation :', err);
      },
    });
  }

  loadPendingConversations(): void {
    this.chatService.getPendingConversations().subscribe({
      next: (convs) => (this.pendingConversations = convs),
      error: (err) => console.error('Erreur conversations en attente :', err),
    });

    this.chatService.getMyConversations(this.currentUser!.userId).subscribe({
      next: (convs) => (this.myConversations = convs),
      error: (err) => console.error('Erreur mes conversations :', err),
    });
  }

  joinConversation(conversationId: number): void {
    this.isConnecting = true;
    this.chatService.assignAgent(conversationId, this.currentUser!.userId).subscribe({
      next: () => {
        this.conversationId = conversationId;
        this.stopPolling();
        this.loadHistoryAndConnect();
      },
      error: (err) => {
        this.isConnecting = false;
        if (err.status === 403) {
          this.errorMessage = 'Cette conversation est déjà prise en charge.';
          this.loadPendingConversations();
        } else {
          this.errorMessage = 'Impossible de rejoindre la conversation.';
          console.error('Erreur assignAgent :', err);
        }
      },
    });
  }

  resumeConversation(conversationId: number): void {
    this.conversationId = conversationId;
    this.stopPolling();
    this.loadHistoryAndConnect();
  }

  loadHistoryAndConnect(): void {
    this.chatService.getHistory(this.conversationId!).subscribe({
      next: (history) => {
        this.messages = history;
        this.isConnecting = false;
        this.shouldScrollToBottom = true;

        this.chatService.connect(this.conversationId!, this.currentUser!.token);

        this.messageSubscription?.unsubscribe();
        this.messageSubscription = this.chatService.getMessages().subscribe((msg) => {
          this.messages.push(msg);
          this.shouldScrollToBottom = true;
        });
      },
      error: (err) => {
        this.isConnecting = false;
        this.errorMessage = "Impossible de charger l'historique.";
        console.error('Erreur getHistory :', err);
      },
    });
  }

  leaveConversation(): void {
    this.messageSubscription?.unsubscribe();
    this.chatService.disconnect();
    this.conversationId = null;
    this.messages = [];
    this.loadPendingConversations();
    this.startPolling();
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.conversationId) return;

    this.chatService.sendMessage(this.conversationId, this.newMessage.trim());
    this.newMessage = '';
  }

  logout(): void {
    this.chatService.disconnect();
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.stopPolling();
    this.messageSubscription?.unsubscribe();
    this.chatService.disconnect();
  }
}
