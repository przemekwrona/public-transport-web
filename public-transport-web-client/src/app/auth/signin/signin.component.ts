import {Component, OnInit} from '@angular/core';
import {LoginService} from "../login.service";
import {LoginAppUserRequest, LoginAppUserResponse} from "../../generated/public-transport-api";
import {Router} from "@angular/router";

@Component({
    selector: 'app-signin',
    templateUrl: './signin.component.html',
    styleUrl: './signin.component.scss',
    standalone: false
})
export class SigninComponent implements OnInit {

    public loginAppUserRequest: LoginAppUserRequest = {};

    public passwordInputType

    constructor(private authService: LoginService, private router: Router) {
    }

    ngOnInit(): void {
        this.setPasswordInputPassword();
    }

    public login() {
        this.authService.login(this.loginAppUserRequest).subscribe((response: LoginAppUserResponse) => {
            this.router.navigate(['/agency/profile']);
        });
    }

    public revertPasswordTypeInput(): void {
        if (this.passwordInputType === 'password') {
            this.setPasswordInputText();
        } else {
            this.setPasswordInputPassword();
        }
    }

    public setPasswordInputPassword() {
        this.passwordInputType = 'password';
    }

    public setPasswordInputText() {
        this.passwordInputType = 'text';
    }
}
