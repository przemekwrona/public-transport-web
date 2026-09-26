import {Component, OnInit} from '@angular/core';
import {LoginService} from "../login.service";
import {Router} from "@angular/router";

@Component({
    selector: 'app-signin',
    templateUrl: './signin.component.html',
    styleUrl: './signin.component.scss',
    standalone: false
})
export class SigninComponent implements OnInit {

    public loginFailed = false;

    constructor(private authService: LoginService, private router: Router) {
    }

    ngOnInit(): void {
        if (this.authService.isLoggedIn()) {
            this.router.navigate(['/agency/profile']);
            return;
        }
        this.login();
    }

    public login(): void {
        this.loginFailed = false;
        this.authService.loginWithKeycloak().catch(() => this.loginFailed = true);
    }
}
