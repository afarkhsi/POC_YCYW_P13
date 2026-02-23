import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';
  // état de chargement pour désactiver le bouton pendant la requête
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  login(): void {
    // validation basique côté client avant d'appeler l'API
    if (!this.email.trim() || !this.password.trim()) {
      this.error = 'Veuillez remplir tous les champs';
      return;
    }

    this.isLoading = true;
    this.error = '';

    this.authService.login(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/chat']),
      // gestion d'erreur avec distinction 401 vs erreur réseau
      error: (err) => {
        this.isLoading = false;
        if (err.status === 401) {
          this.error = 'Email ou mot de passe incorrect';
        } else {
          this.error = 'Erreur de connexion au serveur. Réessayez plus tard.';
        }
      },
    });
  }
}
