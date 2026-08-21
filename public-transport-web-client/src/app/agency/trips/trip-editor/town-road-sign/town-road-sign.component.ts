import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-town-road-sign',
  imports: [],
  templateUrl: './town-road-sign.component.html',
  styleUrl: './town-road-sign.component.scss'
})
export class TownRoadSignComponent {
  @Input() townName = '';
}
