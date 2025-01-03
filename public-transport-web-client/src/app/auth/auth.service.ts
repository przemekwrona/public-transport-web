import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, tap} from "rxjs";
import {LoginAppUserRequest, LoginAppUserResponse} from "../generated/public-transport";

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private token: string = '';

    constructor(private httpClient: HttpClient) {
    }

    login(loginCredentials: LoginAppUserRequest): Observable<LoginAppUserResponse> {
        return this.httpClient.post<LoginAppUserResponse>(`/api/v1/auth/login`, loginCredentials)
            .pipe(tap((response: LoginAppUserResponse) => {
                this.token = response.token || '';
            }));
    }

    logout(): void {
        this.token = '';
    }

}
