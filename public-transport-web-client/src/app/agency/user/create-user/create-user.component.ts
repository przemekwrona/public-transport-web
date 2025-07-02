import {Component} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {Router, RouterModule} from "@angular/router";
import {CommonModule} from "@angular/common";
import {CreateAppUserRequest, UsersService} from "../../../generated/public-transport";
import {NotificationService} from "../../../shared/notification.service";
import {emailExistenceValidator} from "./email-existence.validator";
import {matchPasswordValidator} from "./match-password.validator";

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
            accountEmail: ['', [Validators.required, Validators.email, emailExistenceValidator()]],
            accountPassword: ['', [Validators.required]],
            repeatedPassword: ['', [Validators.required]]
        }, {
            validators: matchPasswordValidator('accountPassword', 'repeatedPassword')
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

    public validityCheckEmail(fieldName: string): boolean {
        return this.validityCheck(fieldName)
            && this.createUserForm.get(fieldName)?.errors?.['email'];
    }

    public validityCheckExistence(fieldName: string): boolean {
        return this.validityCheck(fieldName)
            && this.createUserForm.get(fieldName)?.errors?.['exists'];
    }

    public validityCheckPasswordMismatch(fieldName: string): boolean {
        return this.createUserForm.get(fieldName)?.touched
            && this.createUserForm.errors?.['passwordMismatch'];
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
