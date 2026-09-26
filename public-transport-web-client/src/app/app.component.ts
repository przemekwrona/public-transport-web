import {AfterViewInit, Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {LoginService} from "./auth/login.service";
import {KEYCLOAK_LOGIN_RETURN_KEY} from "./auth/keycloak-auth.service";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrl: './app.component.css',
    standalone: false
})
export class AppComponent implements OnInit, AfterViewInit {
  title = 'public-transport-web-client';

  constructor(private router: Router, private loginService: LoginService) {
  }

  ngAfterViewInit(): void {
  }

  ngOnInit(): void {
    if (sessionStorage.getItem(KEYCLOAK_LOGIN_RETURN_KEY) !== '1') {
      return;
    }
    sessionStorage.removeItem(KEYCLOAK_LOGIN_RETURN_KEY);
    if (this.loginService.isLoggedIn()) {
      this.router.navigate(['/agency/profile']);
    }
  }
}
