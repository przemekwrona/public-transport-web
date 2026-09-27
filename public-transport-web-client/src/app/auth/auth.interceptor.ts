import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpInterceptorFn,
    HttpRequest
} from '@angular/common/http';
import {Injectable} from "@angular/core";
import {catchError, from, Observable, of, switchMap, throwError} from "rxjs";
import {Router} from "@angular/router";
import {KeycloakAuthService} from "./keycloak-auth.service";

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    return next(req);
};

@Injectable()
export class AddHeaderInterceptor implements HttpInterceptor {

    constructor(private router: Router, private keycloakAuth: KeycloakAuthService) {
    }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (!this.keycloakAuth.isLoggedIn()) {
            return this.handle(req, next, '');
        }
        return from(this.keycloakAuth.updateToken(30)).pipe(
            switchMap(token => this.handle(req, next, token)),
            catchError((error: unknown) => {
                if (error instanceof HttpErrorResponse) {
                    return throwError(() => error);
                }
                this.router.navigate(['/signin']);
                return of();
            })
        );
    }

    private handle(req: HttpRequest<any>, next: HttpHandler, token: string): Observable<HttpEvent<any>> {
        const request = token
            ? req.clone({headers: req.headers.set('Authorization', `Bearer ${token}`)})
            : req;
        return next.handle(request).pipe(catchError((error: HttpErrorResponse) => {
            if (error.status === 401 && !this.keycloakAuth.isLoggedIn()) {
                this.router.navigate(['/signin']);
                return of();
            }
            return throwError(() => error);
        }));
    }
}
