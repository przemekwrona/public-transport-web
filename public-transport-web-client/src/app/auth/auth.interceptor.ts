import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandler, HttpHeaders,
    HttpInterceptor,
    HttpInterceptorFn,
    HttpRequest, HttpResponse
} from '@angular/common/http';
import {Injectable} from "@angular/core";
import {Observable, tap} from "rxjs";
import {AuthService} from "./auth.service";

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    return next(req);
};

@Injectable()
export class AddHeaderInterceptor implements HttpInterceptor {

    constructor() {
    }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // Get token
        const token: string = sessionStorage.getItem(AuthService.SESSION_STORAGE_AUTH_TOKEN_KEY) || '';

        // Clone the request to add the new header
        const clonedRequest = req.clone({headers: req.headers.append('Authorization', `Bearer ${token}`)});

        // Pass the cloned request instead of the original request to the next handle
        return next.handle(clonedRequest)
            .pipe(tap((event: HttpEvent<any>) => {
                if (event instanceof HttpResponse) {
                    const headers: HttpHeaders = event.headers;
                }
            }));
    }
}
