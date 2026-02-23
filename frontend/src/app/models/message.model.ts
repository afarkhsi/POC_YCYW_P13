export interface Message {
  conversationId: number;
  senderId: number;
  senderName: string;
  senderType: string;
  message: string;
  createdAt: string;
}
