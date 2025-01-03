import {Component} from '@angular/core';
import {AuthService} from "../auth.service";
import {LoginAppUserRequest} from "../../generated/public-transport";

@Component({
    selector: 'app-signin',
    templateUrl: './signin.component.html',
    styleUrl: './signin.component.scss'
})
export class SigninComponent {

    public loginAppUserRequest: LoginAppUserRequest = {};

    constructor(private authService: AuthService) {
    }

    public login() {
        this.authService.login(this.loginAppUserRequest).subscribe()
    }

}
