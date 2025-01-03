import {Component} from '@angular/core';
import {AuthService} from "../auth.service";
import {LoginAppUserRequest, LoginAppUserResponse} from "../../generated/public-transport";
import {Router} from "@angular/router";

@Component({
    selector: 'app-signin',
    templateUrl: './signin.component.html',
    styleUrl: './signin.component.scss'
})
export class SigninComponent {

    public loginAppUserRequest: LoginAppUserRequest = {};

    constructor(private authService: AuthService, private router: Router) {
    }

    public login() {
        this.authService.login(this.loginAppUserRequest).subscribe((response: LoginAppUserResponse) => {
            this.router.navigate(['/agency/profile']);
        });
    }

}
