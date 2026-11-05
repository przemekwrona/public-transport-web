import { Component } from '@angular/core';
import {TranslocoPipe} from "@jsverse/transloco";

@Component({
  imports: [
    TranslocoPipe
  ],
  selector: 'app-account',
  styleUrl: './account.component.scss',
  templateUrl: './account.component.html',
})
export class AccountComponent {
}
