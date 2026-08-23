import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-town-road-sign',
  imports: [],
  templateUrl: './town-road-sign.component.html',
  styleUrl: './town-road-sign.component.scss'
})
export class TownRoadSignComponent {
  @Input() townName = '';

  get formattedTownName(): string {
    return this.formatTownName(this.townName);
  }

  private formatTownName(name: string): string {
    if (!name) {
      return '';
    }

    return name
      .trim()
      .split(/(\s+)/)
      .map(part => /^\s+$/.test(part)
        ? part
        : part.split('-').map(segment => this.capitalizeWord(segment)).join('-'))
      .join('');
  }

  private capitalizeWord(word: string): string {
    if (!word) {
      return word;
    }

    const lower = word.toLocaleLowerCase('pl-PL');
    return lower.charAt(0).toLocaleUpperCase('pl-PL') + lower.slice(1);
  }
}
