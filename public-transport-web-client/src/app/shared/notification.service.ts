import {Injectable} from '@angular/core';
import {MatSnackBar} from "@angular/material/snack-bar";

@Injectable({
    providedIn: 'root'
})
export class NotificationService {

    constructor(private snackBar: MatSnackBar) {
    }

    showNotification(message: string, action: string = 'Close', panelClass: string = '') {
        // Displaying the toast notification
        this.snackBar.open(message, action, {
            duration: 5000,  // Notification will disappear after 3 seconds
            horizontalPosition: 'right',
            verticalPosition: 'top',
            panelClass: [panelClass]
        });
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
}
