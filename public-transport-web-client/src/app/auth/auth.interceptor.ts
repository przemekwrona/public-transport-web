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

    constructor(private authService: AuthService) {
    }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const token: string = this.authService.getToken();

        // Clone the request to add the new header
        const clonedRequest = req.clone({headers: req.headers.append('Authorization', `Bearer ${token}`)});

        // Pass the cloned request instead of the original request to the next handle
        return next.handle(req)
            .pipe(tap((event: HttpEvent<any>) => {
                if (event instanceof HttpResponse) {
                    const headers: HttpHeaders = event.headers;
                    console.log(headers);
                }
            }));
    }
}
