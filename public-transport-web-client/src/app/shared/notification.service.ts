import {Injectable} from '@angular/core';
import {MatSnackBar} from "@angular/material/snack-bar";

@Injectable({
    providedIn: 'root'
})
export class NotificationService {
    private queue: Array<{ message: string; action?: string; panelClass?: string }> = [];
    private isShowing = false;


    constructor(private snackBar: MatSnackBar) {
    }

    showNotification(message: string, action: string = 'Close', panelClass: string = '') {
        this.queue.push({ message, action, panelClass });
        this.processQueue();
    }

    showSuccess(message: string) {
        this.showNotification(message, '', 'snackbar-success')
    }

    showError(message: string) {
        this.showNotification(message, '', 'snackbar-error')
    }

    showInfo(message: string) {
        this.showNotification(message, '', 'snackbar-info')
    }

    private processQueue() {
        if (this.isShowing || this.queue.length === 0) return;

        this.isShowing = true;
        const { message, action, panelClass } = this.queue.shift()!;

        const ref = this.snackBar.open(message, action, {
            duration: 3000,
            horizontalPosition: 'right',
            verticalPosition: 'top',
            panelClass: [panelClass]
        });

        ref.afterDismissed().subscribe(() => {
            this.isShowing = false;
            this.processQueue();
        });
    }
}
