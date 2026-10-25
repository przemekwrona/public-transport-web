import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-documentation',
  imports: [RouterLink],
  templateUrl: './documentation.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './documentation.component.scss'
})
export class DocumentationComponent {

}
