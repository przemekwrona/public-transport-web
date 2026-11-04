import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {LoginService} from "../login.service";
import {LoginAppUserRequest, LoginAppUserResponse} from "../../generated/public-transport-api";
import {Router} from "@angular/router";
import {NgxSpinnerService} from "ngx-spinner";
import {finalize, map, switchMap, timer} from "rxjs";

@Component({
    selector: 'app-signin',
    templateUrl: './signin.component.html',
    styleUrl: './signin.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export class SigninComponent implements OnInit {

    public loginAppUserRequest: LoginAppUserRequest = {};
    public isLoggingIn = false;

    public passwordInputType

    private readonly spinnerName = 'signin';
    private readonly minimumEffectMs = 2000;
    private navigating = false;

    constructor(private authService: LoginService, private router: Router, private spinner: NgxSpinnerService) {
    }

    ngOnInit(): void {
        this.setPasswordInputPassword();
    }

    public login() {
        if (this.isLoggingIn) {
            return;
        }

        this.isLoggingIn = true;
        this.showSpinner();
        const startedAt = Date.now();

        this.authService.login(this.loginAppUserRequest).pipe(
            switchMap((response: LoginAppUserResponse) => {
                const remaining = Math.max(0, this.minimumEffectMs - (Date.now() - startedAt));
                return timer(remaining).pipe(map(() => response));
            }),
            finalize(() => {
                if (!this.navigating) {
                    this.isLoggingIn = false;
                    this.hideSpinner();
                }
            })
        ).subscribe({
            next: () => {
                this.navigating = true;
                this.router.navigate(['/agency/profile']).then(navigated => {
                    if (!navigated) {
                        this.navigating = false;
                        this.isLoggingIn = false;
                        this.hideSpinner();
                    }
                }).catch(() => {
                    this.navigating = false;
                    this.isLoggingIn = false;
                    this.hideSpinner();
                });
            }
        });
    }

    private showSpinner(): void {
        this.spinner.show(this.spinnerName, {
            type: 'ball-scale-multiple',
            size: 'large',
            bdColor: 'rgba(51,51,51,0.8)',
            color: '#fff',
            fullScreen: true
        });
    }

    private hideSpinner(): void {
        this.spinner.hide(this.spinnerName);
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
