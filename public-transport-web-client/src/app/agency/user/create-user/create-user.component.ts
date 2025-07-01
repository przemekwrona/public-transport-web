import {Component} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {ActivatedRoute, Route, Router, RouterModule} from "@angular/router";
import {CommonModule} from "@angular/common";
import {CreateAppUserRequest, UsersService} from "../../../generated/public-transport";
import {NotificationService} from "../../../shared/notification.service";

@Component({
    selector: 'app-create-user',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule
    ],
    templateUrl: './create-user.component.html',
    styleUrl: './create-user.component.scss'
})
export class CreateUserComponent {

    public createUserForm: FormGroup;

    constructor(private fb: FormBuilder, private userService: UsersService, private notificationService: NotificationService, private router: Router) {
        this.createUserForm = this.fb.group({
            accountName: ['', [Validators.required]],
            accountEmail: ['', [Validators.required]],
            accountPassword: ['', [Validators.required]],
            repeatedPassword: ['', [Validators.required]]
        });
    }

    public validityCheck(fieldName: string): boolean {
        return this.createUserForm.get(fieldName)?.touched
            && this.createUserForm.get(fieldName)?.invalid;
    }

    public validityCheckRequired(fieldName: string): boolean {
        return this.validityCheck(fieldName)
            && this.createUserForm.get(fieldName)?.errors?.['required'];
    }

    public createUser(): void {
        const createAppUserRequest: CreateAppUserRequest = {} as CreateAppUserRequest;
        createAppUserRequest.username = this.createUserForm.controls['accountName'].value;
        createAppUserRequest.email = this.createUserForm.controls['accountEmail'].value;
        createAppUserRequest.password = this.createUserForm.controls['accountPassword'].value;

        this.userService.createUser(createAppUserRequest).subscribe(response => {
            this.notificationService.showSuccess(`Użytkownik ${createAppUserRequest.username} został utworzony`);
            this.router.navigate(['/admin/users']).then(() => {});
        })
    }

}
