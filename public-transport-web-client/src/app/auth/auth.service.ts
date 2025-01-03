import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {LoginAppUserRequest, LoginAppUserResponse} from "../generated/public-transport";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private httpClient: HttpClient) { }

  login(loginCredentials: LoginAppUserRequest): Observable<LoginAppUserResponse> {
    return this.httpClient.post<LoginAppUserResponse>(`/api/v1/auth/login`, loginCredentials);
  }

}
