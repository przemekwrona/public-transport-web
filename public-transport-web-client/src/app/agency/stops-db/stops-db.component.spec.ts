import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StopsDbComponent } from './stops-db.component';

describe('StopsDbComponent', () => {
  let component: StopsDbComponent;
  let fixture: ComponentFixture<StopsDbComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StopsDbComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StopsDbComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
