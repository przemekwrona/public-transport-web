import {Component} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {RouterModule} from "@angular/router";
import {CommonModule} from "@angular/common";

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

    constructor(private fb: FormBuilder) {
        this.createUserForm = this.fb.group({
            accountName: ['', [Validators.required]],
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

    }

}
