import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CitySelectorComponent } from './city-selector.component';

describe('CitySelectorComponent', () => {
  let component: CitySelectorComponent;
  let fixture: ComponentFixture<CitySelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CitySelectorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CitySelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
