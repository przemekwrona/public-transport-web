import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TownRoadSignComponent } from './town-road-sign.component';

describe('TownRoadSignComponent', () => {
  let component: TownRoadSignComponent;
  let fixture: ComponentFixture<TownRoadSignComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TownRoadSignComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TownRoadSignComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
